package com.mrunal.arttour.event;

import com.mrunal.arttour.model.Artwork;

public class ArtSelectedEvent {

  public final Artwork artwork;

  public ArtSelectedEvent(Artwork artwork) {
    this.artwork = artwork;
  }

  public Artwork getSelectedArt() {
    return artwork;
  }

}
